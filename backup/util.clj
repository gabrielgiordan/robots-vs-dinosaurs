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

(defn filter-in-first
  "Reduce with `reduced` function for first element
  is faster than `(first (filter ...))` or `(some ...)`."
  [coll [key val]]
  (when-not (empty? coll)
    (reduce #(when (= (key %2) val) (reduced %2)) nil coll)))

(defn contains-in?
  [coll [key val]]
  (nil? (find-first coll [key val])))

(defn any-in?
  [coll [key val]]
  (nil? (find-first coll [key val])))

(defn conj-disj
  "Returns a function that join the `x` after disjoint a `key` into the `coll`."
  [coll disj-val conj-val]
  (disj (conj coll conj-val) disj-val))