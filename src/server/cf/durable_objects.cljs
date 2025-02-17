(ns server.cf.durable-objects
  (:require [server.cf :as cf]))

;; Docs
;; https://developers.cloudflare.com/durable-objects/api/base/

(defn ^js/DurableObject name->instance
  "Get a Durable Object instance by name.
  name - room id, board id, some unique identifier"
  [binding-name object-name]
  (let [^js binding (aget @cf/ENV (name binding-name))
        id (.idFromName binding object-name)
        stub (.get binding id)]
    stub))

;; Durable Object storage API
;; https://developers.cloudflare.com/durable-objects/api/storage-api/

(defn ^js/Promise storage-get+
  "https://developers.cloudflare.com/durable-objects/api/storage-api/#get"
  [ctx key
   & [{:keys [allow-concurrency no-cache]
       :or {allow-concurrency false
            no-cache false}
       :as opts}]]
  (.get (.-storage ctx) (clj->js key) #js {:allowConcurrency allow-concurrency
                                           :noCache no-cache}))

(defn ^js/Promise storage-put+
  "https://developers.cloudflare.com/durable-objects/api/storage-api/#put"
  [ctx key value
   & [{:keys [allow-unconfirmed no-cache]
       :or {allow-unconfirmed false
            no-cache false}
       :as opts}]]
  (.put (.-storage ctx) (clj->js key) value #js {:allowUnconfirmed allow-unconfirmed
                                                 :noCache no-cache}))

(defn ^js/Promise storage-delete+
  "https://developers.cloudflare.com/durable-objects/api/storage-api/#delete"
  [ctx key
   & [{:keys [allow-unconfirmed no-cache]
       :or {allow-unconfirmed false
            no-cache false}
       :as opts}]]
  (.delete (.-storage ctx) (clj->js key) #js {:allowUnconfirmed allow-unconfirmed
                                              :noCache no-cache}))

(defn ^js/Promise storage-list+
  "https://developers.cloudflare.com/durable-objects/api/storage-api/#list"
  [ctx
   & [{:keys [start start-after env prefix reverse limit allow-concurrency no-cache]
       :or {reverse false
            allow-concurrency false
            no-cache false}
       :as opts}]]
  (.list (.-storage ctx) (clj->js opts)))