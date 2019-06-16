(ns robots-vs-dinosaurs.adapter
  (:require
    (robots-vs-dinosaurs.logic
      [direction :as direction]
      [point :as point]
      [unit :as unit])
    [clojure.edn :as edn]))

(defn string->int [s]
  "Converts a string to an integer."
  (if (number? s)
    s
    (try
      (edn/read-string s)
      (catch Throwable _ nil))))

(defn string->orientation [s]
  "Converts a string to an orientation."
  (when-let [k (find-keyword s)]
    (when (k direction/four-sides)
      k)))

(defn map->Point
  "Converts a map to a Point."
  [{:keys [x y]}]
  (point/new-point x y))

(defn string->dinosaur-subtype
  [s]
  (when-let [k (find-keyword s)]
    (when (k unit/dinosaur-subtype)
      k)))