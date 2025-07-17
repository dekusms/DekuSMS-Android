"""
The ``random_word`` module houses all classes and functions relating to the
generation of single random words.
"""

import random
import re
import enum
from typing import Optional, List

from . import assets

try:
    import importlib.resources as pkg_resources
except ImportError:
    # Try backported to PY<37 `importlib_resources`.
    import importlib_resources as pkg_resources


class NoWordsToChoseFrom(Exception):
    """NoWordsToChoseFrom is raised when there is an attempt to access more
    words than exist. This exception may be raised if the amount of random
    words to generate is larger than the amount of words that exist.
    """

    pass


class Defaults(enum.Enum):
    """This enum represents the default word lists. For example, if you want a
    random word generator with one category labeled 'adj' for adjectives, but
    want to use the default word list, you can do the following::

        >>> from wonderwords import RandomWord, Defaults
        >>> w = RandomWord(adj=Defaults.ADJECTIVES)
        >>> w.word()
        'red'

    Options available:

    * ``Defaults.NOUNS``: Represents a list of nouns
    * ``Defaults.VERBS``: Represents a list of verbs
    * ``Defaults.ADJECTIVES``: Represents a list of adjectives
    * ``Defaults.PROFANITIES``: Represents a list of curse words

    """

    NOUNS = "nounlist.txt"
    VERBS = "verblist.txt"
    ADJECTIVES = "adjectivelist.txt"
    PROFANITIES = "profanitylist.txt"


def _load_default_categories(default_categories):
    """Load all the default word lists"""
    out = {}
    for category in default_categories:
        out[category] = _get_words_from_text_file(category.value)
    return out


def _get_words_from_text_file(word_file):
    """Read a file found in static/ where each line has a word, and return
    all words as a list
    """
    words = pkg_resources.open_text(assets, word_file).readlines()
    return [word.rstrip() for word in words]


_default_categories = _load_default_categories(Defaults)


class RandomWord:
    """The RandomWord class encapsulates multiple methods dealing with the
    generation of random words and lists of random words.

    Example::

        >>> from wonderwords import RandomWord, Defaults
        >>>
        >>> r = RandomWord(noun=["apple", "orange"]) # Category 'noun' with
        ...     # the words 'apple' and 'orange'
        >>> r2 = RandomWord() # Use the default word lists
        >>> r3 = RandomWord(noun=Defaults.NOUNS) # Set the category 'noun' to
        ...     # the default list of nouns

    .. important::

       Wonderwords version ``2.0`` does not have custom
       categories. In fact there are only three categories: nouns, verbs, and
       adjectives. However, wonderwords will remain backwards compatible until
       version ``3``. Please note that the ``parts_of_speech`` attribute will
       soon be deprecated, along with other method-specific features.

    :param \*\*kwargs: keyword arguments where each key is a category of words
        and value is a list of words in that category. You can also use a
        default list of words by using the `Default` enum instead.
    :type nouns: list, optional

    """

    def __init__(self, **kwargs):
        if kwargs:
            self._categories = self._custom_categories(kwargs)
        else:
            self._categories = self._custom_categories(
                {
                    "noun": Defaults.NOUNS,
                    "verb": Defaults.VERBS,
                    "adjective": Defaults.ADJECTIVES,
                    # The following was added for backwards compatibility
                    # reasons. The plural categories will be deleted in
                    # wonderwords version 3. See issue #9.
                    "nouns": Defaults.NOUNS,
                    "verbs": Defaults.VERBS,
                    "adjectives": Defaults.ADJECTIVES,
                }
            )
        # Kept for backwards compatibility
        self.parts_of_speech = self._categories

    def filter(  # noqa: C901
        self,
        starts_with: str = "",
        ends_with: str = "",
        include_categories: Optional[List[str]] = None,
        include_parts_of_speech: Optional[List[str]] = None,
        word_min_length: Optional[int] = None,
        word_max_length: Optional[int] = None,
        regex: Optional[str] = None,
    ):
        """Return all existing words that match the criteria specified by the
        arguments.

        Example::

            >>> # Filter all nouns that start with a:
            >>> r.filter(starts_with="a", include_categories=["noun"])

        .. important:: The ``include_parts_of_speech`` argument will soon be
            deprecated. Use ``include_categories`` which performs the exact
            same role.

        :param starts_with: the string each word should start with. Defaults to
            "".
        :type starts_with: str, optional
        :param ends_with: the string each word should end with. Defaults to "".
        :type ends_with: str, optional
        :param include_categories: a list of strings denoting a part of
            speech. Each word returned will be in the category of at least one
            part of speech. By default, all parts of speech are enabled.
            Defaults to None.
        :type include_categories: list of strings, optional
        :param include_parts_of_speech: Same as include_categories, but will
            soon be deprecated.
        :type include_parts_of_speech: list of strings, optional
        :param word_min_length: the minimum length of each word. Defaults to
            None.
        :type word_min_length: int, optional
        :param word_max_length: the maximum length of each word. Defaults to
            None.
        :type word_max_length: int, optional
        :param regex: a custom regular expression which each word must fully
            match (re.fullmatch). Defaults to None.
        :type regex: str, optional

        :return: a list of unique words that match each of the criteria
            specified
        :rtype: list of strings
        """
        word_min_length, word_max_length = self._validate_lengths(
            word_min_length, word_max_length
        )

        # include_parts_of_speech will be deprecated in a future release
        if not include_categories:
            if include_parts_of_speech:
                include_categories = include_parts_of_speech
            else:
                include_categories = self._categories.keys()

        # filter parts of speech
        words = []
        for category in include_categories:
            try:
                words.extend(self._categories[category])
            except KeyError:
                raise ValueError(
                    f"'{category}' is an invalid category"
                ) from None

        # starts/ends
        if starts_with != "" or ends_with != "":
            for word in words[:]:
                if not word.startswith(starts_with):
                    words.remove(word)
                elif not word.endswith(ends_with):
                    words.remove(word)

        # length
        if word_min_length is not None or word_max_length is not None:
            for word in words[:]:
                if word_min_length is not None and len(word) < word_min_length:
                    words.remove(word)
                elif (
                    word_max_length is not None
                    and len(word) > word_max_length
                ):
                    words.remove(word)

        # regex
        if regex is not None:
            words = [
                word for word in words if re.fullmatch(regex, word) is not None
            ]

        return list(set(words))

    def random_words(
        self,
        amount: int = 1,
        starts_with: str = "",
        ends_with: str = "",
        include_categories: Optional[List[str]] = None,
        include_parts_of_speech: Optional[List[str]] = None,
        word_min_length: Optional[int] = None,
        word_max_length: Optional[int] = None,
        regex: Optional[str] = None,
        return_less_if_necessary: bool = False,
    ):
        """Generate a list of n random words specified by the ``amount``
        parameter and fit the criteria specified.

        Example::

            >>> # Generate a list of 3 adjectives or nouns which start with
            ...     # "at"
            >>> # and are at least 2 letters long
            >>> r.random_words(
            ...     3,
            ...     starts_with="at",
            ...     include_parts_of_speech=["adjectives", "nouns"],
            ...     word_min_length=2
            ... )

        :param amount: the amount of words to generate. Defaults to 1.
        :type amount: int, optional
        :param starts_with: the string each word should start with. Defaults to
            "".
        :type starts_with: str, optional
        :param ends_with: the string each word should end with. Defaults to "".
        :type ends_with: str, optional
        :param include_categories: a list of strings denoting a part of
            speech. Each word returned will be in the category of at least one
            part of speech. By default, all parts of speech are enabled.
            Defaults to None.
        :type include_categories: list of strings, optional
        :param include_parts_of_speech: Same as include_categories, but will
            soon be deprecated.
        :type include_parts_of_speech: list of strings, optional
        :param word_min_length: the minimum length of each word. Defaults to
            None.
        :type word_min_length: int, optional
        :param word_max_length: the maximum length of each word. Defaults to
            None.
        :type word_max_length: int, optional
        :param regex: a custom regular expression which each word must fully
            match (re.fullmatch). Defaults to None.
        :type regex: str, optional
        :param return_less_if_necessary: if set to True, if there aren't enough
            words to statisfy the amount, instead of raising a
            NoWordsToChoseFrom exception, return all words that did statisfy
            the original query.
        :type return_less_if_necessary: bool, optional

        :raises NoWordsToChoseFrom: if there are less words to choose from than
            the amount that was requested, a NoWordsToChoseFrom exception is
            raised, **unless** return_less_if_necessary is set to True.

        :return: a list of the words
        :rtype: list of strings
        """
        choose_from = self.filter(
            starts_with=starts_with,
            ends_with=ends_with,
            include_categories=include_categories,
            include_parts_of_speech=include_parts_of_speech,
            word_min_length=word_min_length,
            word_max_length=word_max_length,
            regex=regex,
        )

        if not return_less_if_necessary and len(choose_from) < amount:
            raise NoWordsToChoseFrom(
                "There aren't enough words to choose from. Cannot generate "
                f"{str(amount)} word(s)"
            )
        elif return_less_if_necessary:
            random.shuffle(choose_from)
            return choose_from

        words = []
        for _ in range(amount):
            new_word = random.choice(choose_from)
            choose_from.remove(new_word)
            words.append(new_word)

        return words

    def word(
        self,
        starts_with: str = "",
        ends_with: str = "",
        include_categories: Optional[List[str]] = None,
        include_parts_of_speech: Optional[List[str]] = None,
        word_min_length: Optional[int] = None,
        word_max_length: Optional[int] = None,
        regex: Optional[str] = None,
    ):
        """Returns a random word that fits the criteria specified by the
        arguments.

        Example::

            >>> # Select a random noun that starts with y
            >>> r.word(ends_with="y", include_parts_of_speech=["nouns"])

        :param starts_with: the string each word should start with. Defaults to
            "".
        :type starts_with: str, optional
        :param ends_with: the string the word should end with. Defaults to "".
        :type ends_with: str, optional
        :param include_categories: a list of strings denoting a part of
            speech. Each word returned will be in the category of at least one
            part of speech. By default, all parts of speech are enabled.
            Defaults to None.
        :type include_categories: list of strings, optional
        :param include_parts_of_speech: Same as include_categories, but will
            soon be deprecated.
        :type include_parts_of_speech: list of strings, optional
        :param word_min_length: the minimum length of the word. Defaults to
            None.
        :type word_min_length: int, optional
        :param word_max_length: the maximum length of the word. Defaults to
            None.
        :type word_max_length: int, optional
        :param regex: a custom regular expression which each word must fully
            match (re.fullmatch). Defaults to None.
        :type regex: str, optional

        :raises NoWordsToChoseFrom: if a word fitting the criteria doesn't
            exist

        :return: a word
        :rtype: str
        """
        return self.random_words(
            amount=1,
            starts_with=starts_with,
            ends_with=ends_with,
            include_categories=include_categories,
            include_parts_of_speech=include_parts_of_speech,
            word_min_length=word_min_length,
            word_max_length=word_max_length,
            regex=regex,
        )[0]

    @staticmethod
    def read_words(word_file):
        """Will soon be deprecated. This method isn't meant to be public, but
        will remain for backwards compatibility. Developers: use
        _get_words_from_text_file internally instead.
        """
        return _get_words_from_text_file(word_file)

    def _validate_lengths(self, word_min_length, word_max_length):
        """Validate the values and types of word_min_length and word_max_length
        """
        if not isinstance(word_min_length, (int, type(None))):
            raise TypeError("word_min_length must be type int or None")

        if not isinstance(word_max_length, (int, type(None))):
            raise TypeError("word_max_length must be type int or None")

        if word_min_length is not None and word_max_length is not None:
            if word_min_length > word_max_length and word_max_length != 0:
                raise ValueError(
                    "word_min_length cannot be greater than word_max_length"
                )

        if word_min_length is not None and word_min_length < 0:
            word_min_length = None

        if word_max_length is not None and word_max_length < 0:
            word_max_length = None

        return (word_min_length, word_max_length)

    def _custom_categories(self, custom_categories):
        """Add custom categries of words"""
        out = {}
        for name, words in custom_categories.items():
            if isinstance(words, Defaults):
                out[name] = _default_categories[words]
            else:
                out[name] = words
        return out
