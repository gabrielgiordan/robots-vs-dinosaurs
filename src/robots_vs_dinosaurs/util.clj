(ns robots-vs-dinosaurs.util)

(defn key-of
  "Gets the key of the given `val` value into the `map`."
  [map val]
  (first (keep #(when (= (val %) val) (key %)) map)))

(defn key-value-of
  "Returns a function that gets the first two map `m` values,
  and compare to the given map."
  [map]
  (fn
    [parameters]
    (let [values (vec (vals parameters))]
      (= (values 0) (key-of map (values 1))))))