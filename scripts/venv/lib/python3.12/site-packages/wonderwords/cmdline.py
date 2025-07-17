from rich.console import Console
from rich.panel import Panel
from rich.text import Text
from rich.emoji import Emoji
from rich.padding import Padding
from rich.markdown import Markdown

from . import __version__


console = Console()

AVAILABLE_COMMANDS = """## Available Commands

* `wonderwords -w` - generate a random word
* `wonderwords -f` - get all words matching a certain criteria
* `wonderwords -l AMOUNT` - get a list of `AMOUNT` random words
* `wonderwords -s SENT_TYPE` - generate a random sentence of a certain type

For a list of all options, type `wonderwords -h`. To see a detailed and
comprehensive explanation of the commands, visit
[the documentation](https://wonderwords.readthedocs.io)
"""


class WonderwordsCommandLine:
    def print_title(self):
        title = Panel(
            Text(f"WONDERWORDS {__version__}", justify="center"),
            padding=1,
            style="bold navy_blue on white",
        )
        console.print(title)

    def print_commands(self):
        commands = Markdown(AVAILABLE_COMMANDS)
        console.print(commands)

    def version(self):
        console.print(
            f"Running wonderwords version {__version__}", style="navy_blue on"
            "white"
        )

    def intro(self):
        self.print_title()
        info_text = Text(
            f"No commands given {Emoji('disappointed_face')}",
            justify="center",
            style="bold",
        )
        console.print(Padding(info_text, pad=1))
        self.print_commands()

    def word(self, word):
        word_text = Text(word, style="bold white on navy_blue")
        console.print(word_text)

    def words(self, words, delimiter):
        word_text = Text(
            delimiter.join(words), style="bold white on navy_blue"
        )
        console.print(word_text)

    def sentence(self, sent):
        sent_text = Text(sent, style="bold white on dark_green")
        console.print(sent_text)

    def no_word(self):
        console.print(
            "A word with the parameters specified does not exist! :anguished:",
            style="white on red",
        )

    def no_words(self):
        console.print(
            (
                "There weren't enough words that matched your request. All"
                " words available are listed below :anguished: "
            ),
            style="white on red",
        )
