(ns robots-vs-dinosaurs.board-test
  (:require
    [robots-vs-dinosaurs.board :as b]
    [clojure.test :refer :all]
    [clojure.test.check.generators :as gen]))

(deftest test-board
  (let [width 50
        height 50
        board-2d (b/make-board-2d width height)]

    (testing "edges of `x` and `y` position"
      (let [min-x 0
            min-y 0
            max-x (dec width)
            max-y (dec height)]

        (are [expected position] (= expected (b/get-at board-2d position))
          b/default-data {:x min-x :y min-y},
          b/default-data {:x max-x :y min-y},
          b/default-data {:x min-x :y max-y},
          b/default-data {:x max-x :y max-y})))

    (testing "")))