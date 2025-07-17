import argparse

from .random_word import RandomWord, NoWordsToChoseFrom
from .random_sentence import RandomSentence
from .cmdline import WonderwordsCommandLine


def main():
    parser = argparse.ArgumentParser(
        prog="wonderwords",
        description="""Generate random words and sentences from the command line.
        Here is a full list of available commands. To learn more about each
        command, go to the documentation at https://wonderwords.readthedocs.io
        """,
        epilog="""Thanks to all contributors who made this possible.
        To contribute, go to https://github.com/mrmaxguns/wonderwordsmodule
        """,
    )

    #
    # Base commands
    #
    parser.add_argument(
        "-w",
        "--word",
        "--random-word",
        action="store_true",
        help="generate a random word",
    )

    parser.add_argument(
        "-f",
        "--filter",
        action="store_true",
        help="filter a list of words matching the criteria specified",
    )

    parser.add_argument(
        "-l",
        "--list",
        action="store",
        type=int,
        help="return a list of words of a certain length",
    )

    parser.add_argument(
        "-s",
        "--sentence",
        action="store",
        type=str,
        choices=[
            "bb",
            "ss",
            "bba",
            "s",
        ],
        help="return a sentence based on the structure chosen",
    )

    parser.add_argument(
        "-v",
        "--version",
        action="store_true",
        help="Print the version number and exit"
    )

    #
    # Settings and modifiers
    #
    parser.add_argument(
        "-sw",
        "--starts-with",
        action="store",
        default="",
        type=str,
        help="specify what string the random word(s) should start with",
    )

    parser.add_argument(
        "-ew",
        "--ends-with",
        action="store",
        default="",
        type=str,
        help="specify what string the random word(s) should end with",
    )

    parser.add_argument(
        "-p",
        "--parts-of-speech",
        action="store",
        type=str,
        nargs="+",
        # The plural forms will be removed in version 3
        choices=["noun", "verb", "adjective", "nouns", "verbs", "adjectives"],
        help=(
            "specify to only include certain parts of speech (by default all"
            " parts of speech are included)"
        ),
    )

    parser.add_argument(
        "-min",
        "--word-min-length",
        action="store",
        type=int,
        help="specify the minimum length of the word(s)",
    )

    parser.add_argument(
        "-max",
        "--word-max-length",
        action="store",
        type=int,
        help="specify the maximum length of the word(s)",
    )

    parser.add_argument(
        "-r",
        "--regex",
        "--re",
        "--regular-expression",
        action="store",
        type=str,
        help=(
            "specify a python-style regular expression that every word must"
            " match"
        ),
    )

    parser.add_argument(
        "-d",
        "--delimiter",
        default=", ",
        type=str,
        help=(
            "Specify the delimiter to put between a list of words, default is"
            " ', '"
        ),
    )

    args = parser.parse_args()
    mode = get_mode(args)
    handle_mode(mode, args)


def get_mode(arguments):
    if arguments.version:
        MODE = "version"
    elif arguments.word:
        MODE = "word"
    elif arguments.filter:
        MODE = "filter"
    elif arguments.list is not None:
        MODE = "list"
    elif arguments.sentence is not None:
        MODE = "sentence"
    else:
        MODE = None

    return MODE


def handle_mode(mode, arguments):  # noqa: C901
    command_line = WonderwordsCommandLine()

    if mode == "version":
        command_line.version()
        quit()

    if mode == "word" or mode == "filter" or mode == "list":
        word_parser = RandomWord()

    if mode == "sentence":
        sent_parser = RandomSentence()

    if mode == "word":
        try:
            word = word_parser.word(
                starts_with=arguments.starts_with,
                ends_with=arguments.ends_with,
                include_categories=arguments.parts_of_speech,
                word_min_length=arguments.word_min_length,
                word_max_length=arguments.word_max_length,
                regex=arguments.regex,
            )
        except NoWordsToChoseFrom:
            command_line.no_word()
            quit()

        command_line.word(word)
    elif mode == "filter":
        words = word_parser.filter(
            starts_with=arguments.starts_with,
            ends_with=arguments.ends_with,
            include_categories=arguments.parts_of_speech,
            word_min_length=arguments.word_min_length,
            word_max_length=arguments.word_max_length,
            regex=arguments.regex,
        )

        command_line.words(words, delimiter=arguments.delimiter)

    elif mode == "list":
        try:
            words = word_parser.random_words(
                amount=arguments.list,
                starts_with=arguments.starts_with,
                ends_with=arguments.ends_with,
                include_categories=arguments.parts_of_speech,
                word_min_length=arguments.word_min_length,
                word_max_length=arguments.word_max_length,
                regex=arguments.regex,
            )
        except NoWordsToChoseFrom:
            command_line.no_words()
            words = word_parser.random_words(
                amount=arguments.list,
                starts_with=arguments.starts_with,
                ends_with=arguments.ends_with,
                include_categories=arguments.parts_of_speech,
                word_min_length=arguments.word_min_length,
                word_max_length=arguments.word_max_length,
                return_less_if_necessary=True,
                regex=arguments.regex,
            )

        command_line.words(words, delimiter=arguments.delimiter)
    elif mode == "sentence":
        if arguments.sentence == "bb":
            command_line.sentence(sent_parser.bare_bone_sentence())
        elif arguments.sentence == "ss":
            command_line.sentence(sent_parser.simple_sentence())
        elif arguments.sentence == "bba":
            command_line.sentence(sent_parser.bare_bone_with_adjective())
        else:
            command_line.sentence(sent_parser.sentence())
    else:
        command_line.intro()


if __name__ == "__main__":
    main()
