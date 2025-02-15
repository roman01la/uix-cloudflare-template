(ns app.hooks
  (:require [uix.core :as uix]
            [lib.async :refer [js-await]]))

(defn use-fetch
  ([f]
   (let [[state set-state] (uix/use-state nil)
         f (uix/use-callback
             #(js-await [data (f)]
                (set-state data))
             [f])]
     (uix/use-effect
       #(f)
       [f])
     [state f]))
  ([f params]
   (let [[state set-state] (uix/use-state nil)
         f (uix/use-callback
             #(js-await [data (f params)]
                (set-state data))
             [f params])]
     (uix/use-effect
       #(f)
       [f params])
     [state f])))
