import 'reveal.js/dist/reveal.css'
import 'reveal.js/dist/theme/solarized.css'
import Reveal from 'reveal.js'
import Markdown from 'reveal.js/plugin/markdown/markdown.esm.js';
import HighLight from 'reveal.js/plugin/highlight/highlight.esm.js';
import Zoom from 'reveal.js/plugin/zoom/zoom.esm.js';
import RevealNotes from 'reveal.js/plugin/notes/notes.esm.js';
import Config from '/config.js';

const deck = new Reveal()
deck.initialize({ hash: true, slideNumber: true, pdfSeparateFragments: false, width: Config.width, height: Config.height, margin: Config.margin, maxScale: 3.0, plugins: [ Markdown, HighLight, RevealNotes, Zoom ] })