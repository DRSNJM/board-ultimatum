(ns board-ultimatum.engine.neural
    (use [enclog nnets training]))

(def net
  (network  (neural-pattern :feed-forward)
    :activation :sigmoid
    :input   200
    :output  1
    :hidden [200 100]))
