(ns robots-vs-dinosaurs.logic.scoreboard)

(defrecord Scoreboard [total])

(defonce default-score 10)

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

(defn total*
  "Multiplies the score."
  [scoreboard n]
  (total+ scoreboard (* n default-score)))