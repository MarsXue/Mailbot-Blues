# SMD Project PartA Tester
# By Zijun Chen(Zed)
# Version 0.2


import subprocess
from functools import reduce


def run_once(seed):
    res = subprocess.check_output(["java", "-cp", "./classes/robot.jar:./bin:", "automail.Simulation", str(seed)], stderr=subprocess.STDOUT).decode("utf-8")
    lines = list(filter(None, res.split("\n")))
    last_line = lines[-1]
    if "Final Score:" not in last_line:
        return -1
    score = float(last_line[len("Final Score: "):])
    return score


def main():
#    seeds = [30006, 498519, 45677, 31244, 56784, 78563445, 4, 250, 666, 3750]
    seeds = range(100)
    scores = []
    for seed in seeds:
        score = run_once(seed)
        if score == -1:
            print('ERROR where seed =', seed)
            return
        print('seed =', seed, 'score =', score)
        scores.append(score)

    print('--------')
    print('avg =', reduce(lambda x, y: x + y, scores) / len(scores))

if __name__ == "__main__":
    main()
