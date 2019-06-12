(ns robots-vs-dinosaurs.adapter
  (:require
    (robots-vs-dinosaurs.logic
      [direction :as direction])
    [robots-vs-dinosaurs.logic.point :as point]))

(defn string->int [s]
  (if (number? s)
    s
    (some->>
      s
      (re-find #"\A-?\d+")
      (Integer/parseInt))))

(defn string->orientation [s]
  (when-let [k (find-keyword s)]
    (when (k direction/four-sides)
      k)))

(defn map->point
  [{:keys [x y]}]
  (point/new-point x y))