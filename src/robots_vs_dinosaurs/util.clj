(ns robots-vs-dinosaurs.util)

(defn key-of
  "Gets the key of the given `val` value into the `map`."
  [m v]
  (first (keep #(when (= (v %) v) (key %)) m)))

(defn key-value-of
  "Returns a function that gets the first two map `m` values,
  and compare to the given map."
  [m]
  (fn
    [parameters]
    (let [v (vec (vals parameters))]
      (= (v 0) (key-of m (v 1))))))

(defn find-first
  "Reduce with `reduced` function for first element
  is faster than `(first (filter ...))` or `(some ...)`."
  [coll [k v]]
  (when (seq coll)
    (reduce #(when (= (k %2) v) (reduced %2)) nil coll)))