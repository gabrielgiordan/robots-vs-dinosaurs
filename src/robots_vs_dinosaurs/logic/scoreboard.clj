(ns robots-vs-dinosaurs.logic.scoreboard)

(defrecord Scoreboard [total])

(defonce scores
         {:dinosaur {:vita 5
                     :doux 10
                     :tard 15
                     :mort 25}})
;;
;; Scoreboard
;;
(defn new-scoreboard
  "Creates a new scoreboard."
  ([n]
   (map->Scoreboard
     {:total n}))
  ([]
   (new-scoreboard 0)))

(defn- total+
  "Updates the scoreboard sum total."
  [scoreboard n]
  (update scoreboard :total + n))

(defn sum-score
  "Updates the scoreboard according
  to the attacked units."
  [scoreboard units]
  (total+
    scoreboard
    (reduce
      (fn [total {:keys [type subtype]}]
        (if-let [score (get-in scores [type subtype])]
          (+ total score)
          total)) 0 units)))