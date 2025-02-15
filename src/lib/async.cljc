(ns lib.async
  #?(:cljs (:require-macros [lib.async]))
  (:require [shadow.cljs.modern]))

#?(:clj
   (defmacro js-await [[a b & bindings] & body]
     (let [b `(~'js/Promise.resolve ~b)]
       (if (seq bindings)
         `(shadow.cljs.modern/js-await ~[a b] (js-await ~bindings ~@body))
         `(shadow.cljs.modern/js-await ~[a b] ~@body)))))