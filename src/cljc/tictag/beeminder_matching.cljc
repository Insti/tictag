(ns tictag.beeminder-matching
  (:require [taoensso.timbre :as timbre]))

(defn valid? [thing]
  (cond
    (coll? thing) (let [[c & args] thing]
                    (and (#{'and 'or :and :or} c)
                         (every? valid? args)))
    (keyword? thing) true
    (string? thing) true))

(defn match? [thing b]
  (cond
    (coll? thing)    (let [[pred & args] thing]
                       (case pred
                         (and :and) (every? #(match? % b) args)
                         (or :or)   (some #(match? % b) args)
                         false))
    (keyword? thing) (b (name thing))
    (string? thing)  (b thing)))
