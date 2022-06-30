import 'reveal.js/dist/reveal.css'
import 'reveal.js/dist/theme/solarized.css'
import Reveal from 'reveal.js'
import Markdown from 'reveal.js/plugin/markdown/markdown.esm.js';

const deck = new Reveal()
deck.initialize({ hash: true, slideNumber: true, plugins: [ Markdown ] })