(ns robots-vs-dinosaurs.controller
  (:require [robots-vs-dinosaurs.logic.board :as board]
            [robots-vs-dinosaurs.logic.scoreboard :as score]))

;(def scoreboard-ref (ref (score/create-scoreboard)))
;(def board-ref (ref (board/create-board 50 50)))
;
;(declare get-board)
;
;(defn- get-board-body
;  "Gets the board map."
;  [board]
;  {:size
;    {:width (:width board)
;     :height (:height board)}})
;
;(defn new-board
;  "Creates a new board."
;  [width height]
;   (dosync
;     ;; Also can use ref-set here.
;     (let [scoreboard (alter scoreboard-ref (constantly (score/create-scoreboard)))
;           board (alter board-ref (constantly (board/create-board width height)))]
;       (get-board-body board))))
;
;(defn get-scoreboard
;  []
;  @scoreboard-ref)
;
;(defn get-board
;  []
;  @board-ref)