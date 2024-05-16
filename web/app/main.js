import 'reveal.js/dist/reveal.css'
import 'reveal.js/dist/theme/solarized.css'
import Reveal from 'reveal.js'
import Markdown from 'reveal.js/plugin/markdown/markdown.esm.js';
import HighLight from 'reveal.js/plugin/highlight/highlight.esm.js';
import RevealNotes from 'reveal.js/plugin/notes/notes.esm.js';

const deck = new Reveal()
deck.initialize({ hash: true, slideNumber: true, transition: 'fade', pdfSeparateFragments: false , plugins: [ Markdown, HighLight, RevealNotes ] })