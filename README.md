# uix-cloudflare-template

A template project demonstrating a web app built in UIx/React hosting on Cloudflare, with REST API served from Cloudflare worker and data stored in SQLite (Cloudflare D1).

## Tech
- [Cloudflare Workers](https://workers.cloudflare.com/) (serverless functions)
- [Cloudflare D1](https://developers.cloudflare.com/d1/) (SQLite)
- UIx/React
- [TailwindCSS](https://tailwindcss.com/) and [DaisyUI](https://daisyui.com/)
- [React Query v4](https://tanstack.com/query/v4/) (data fetching)
- [Reitit](https://github.com/metosin/reitit) (routing)
- [Honey SQL](https://github.com/seancorfield/honeysql) (data DSL)

## Project structure
- `src/app` — frontend code
  - `app.core` — UI code 
  - `app.api` — REST API code
  - `app.hooks` — React hooks
- `src/server` — Cloudflare/backend code
  - `server.core` — entry point, API handlers and routing (via [reitit](https://github.com/metosin/reitit))
  - `server.db` — database library
  - `server.cf` — wrappers for Cloudflare API
- `lib.async` — helpers to write async code

## Setup
1. Create [Cloudflare account](https://www.cloudflare.com/)
2. Log into CLI with your account via `npx wrangler login` ([wrangler docs](https://developers.cloudflare.com/workers/wrangler/))
3. Create DB instance `npx wrangler d1 create DB` and update `wrangler.toml` with generated db info ([wrangler config docs](https://developers.cloudflare.com/workers/wrangler/configuration/))

## Create database
- Apply db schema to local instance `npx wrangler d1 execute DB --file=./resources/schema.sql`
- And to production/remote instance `npx wrangler d1 execute DB --remote --file=./resources/schema.sql`

## Development
```shell
npm i # install NPM deps
npm run dev # run dev build in watch mode with CLJS REPL
npx wrangler dev # run Cloudflare server at http://localhost:8787
```

## Production
```shell
npm run release # build production bundle
npx wrangler deploy # deploy to Cloudflare 
```
