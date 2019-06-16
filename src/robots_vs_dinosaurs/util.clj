(ns robots-vs-dinosaurs.util)

(defn find-first
  "Reduce with `reduced` function for first element
  is faster than `(first (filter ...))` or `(some ...)`."
  [coll [k v]]
  (when (seq coll)
    (reduce #(when (= (k %2) v) (reduced %2)) nil coll)))