(ns lib.async
  #?(:cljs (:require-macros [lib.async]))
  (:require [shadow.cljs.modern]))

#?(:clj
   (defmacro js-await
     "Like `let` but for async values, executed sequentially.
     Non-async values are wrapped in `js/Promise.resolve`.
     Returns the result of the last expression wrapped in a promise.
     (js-await [x (async-fn-1)
                y (do-stuff-syn x)
                z (async-fn-2 y)]
        (do-something z))"
     [[a b & bindings] & body]
     (let [b `(~'js/Promise.resolve ~b)]
       (if (seq bindings)
         `(shadow.cljs.modern/js-await ~[a b] (js-await ~bindings ~@body))
         `(shadow.cljs.modern/js-await ~[a b] ~@body)))))