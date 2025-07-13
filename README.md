# AI ASCII Adventure powered by Java and Ollama

## YouTube

For a very detailed explanation about this repository, check out
the [live coding sessions on YouTube](https://youtube.com/live/2P7NASv-LdE).

## Ollama

There's a [docker-compose file](./Docker/docker-compose.yml) that boots up an ollama docker and downloads the llama3.2
model.

## Current features

* Async talking to LLM
* Input hero name
* Create a story through the LLM and make some choices
* Added memory to the LLM to actually make the game playable

## Planned features

* Allow only number inputs
* ASCII Art generation
* Function calling to adjust Health, Mana and Inventory
* Allow using items from inventory
* Clear goal of the game
* Make it possible to loose the game