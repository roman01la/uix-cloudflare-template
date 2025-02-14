# uix-cloudflare-template

A template project demonstrating a web app built in UIx/React hosting on Cloudflare, with REST API served from Cloudflare worker and data stored in SQLite (Cloudflare D1).

## Project structure
- `src/app` — frontend code
  - `app.core` — UI code 
  - `app.api` — REST API code
  - `app.hooks` — React hooks
- `src/server` — Cloudflare/backend code
  - `server.core` — entry point, API handlers and routing
  - `server.db` — database library
  - `server.cf` — wrappers for Cloudflare API

## Setup
1. Create Cloudflare account
2. Log into CLI with your account via `npx wrangler login`
3. Create DB instance `npx wrangler d1 create DB` and update `wrangler.toml` with generated db info

## Create database
- Apply db schema to local instance `npx wrangler d1 execute DB --file=./resources/schema.sql`
- And to production/remote instance `npx wrangler d1 execute DB --remote --file=./resources/schema.sql`

## Development
```shell
npm i # install NPM deps
npm run dev # run dev build in watch mode with CLJS REPL
npx wrangler dev # run Cloudflare server
```

## Production
```shell
npm run release # build production bundle
npx wrangler deploy # deploy to Cloudflare 
```
