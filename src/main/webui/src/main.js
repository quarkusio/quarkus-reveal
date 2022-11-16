import 'reveal.js/dist/reveal.css'
import 'reveal.js/dist/theme/solarized.css'
import 'reveal.js/plugin/highlight/monokai.css'
import 'reveal.js/plugin/highlight/zenburn.css'
import './main.css'
import Reveal from 'reveal.js'
import Markdown from 'reveal.js/plugin/markdown/markdown.esm.js';
import HighLight from 'reveal.js/plugin/highlight/highlight.esm.js';

const deck = new Reveal()
deck.initialize({ hash: true, slideNumber: true, transition: 'fade', plugins: [ Markdown, HighLight ] })