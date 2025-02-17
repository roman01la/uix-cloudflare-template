(ns server.cf
  (:require [shadow.cljs.modern]))

(defn- constructor? [form]
  (when (and (list? form)
             (= 'constructor (first form)))
    form))

(defmacro defclass [name fields & specs]
  (let [[_ args & body] (some constructor? specs)
        specs (remove constructor? specs)
        specs (when-not (and (= 1 (count specs))
                             (= 'Object (first specs)))
                specs)]
    `(shadow.cljs.modern/defclass ~name
       (~'extends ~(:extends (meta name)))

       ~@(for [field fields]
           (if (contains? (meta field) :default)
             `(~'field ~field ~(:default (meta field)))
             `(~'field ~field)))

       (~'constructor ~(or args ['this])
         ~@body)

       ~@specs)))