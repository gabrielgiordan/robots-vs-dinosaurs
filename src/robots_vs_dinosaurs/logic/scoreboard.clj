(ns robots-vs-dinosaurs.logic.scoreboard)

(defrecord Scoreboard [total])

(defn new-scoreboard
  ([n]
   (->Scoreboard n))
  ([]
   (new-scoreboard 0)))

(defn total+
  [data n]
  (update-in data [:total] + n))