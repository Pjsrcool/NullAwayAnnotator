import os
import json
import re
import random

config = json.load(open("../config.json", "r"))
projects = json.load(open("../projects.json", "r"))
root = "/Users/nima/Developer/NullAwayFixer/Projects/{}"
infos = {}
branches = ["dummy", "nullaway"] + ["deep_{}".format(i) for i in range(0, 11)]


# helps to make sure at every 4 line we have one error
def helper(path):
    lines = open(path, "r").readlines()
    for i, l in enumerate(lines):
        if i % 4 == 3 and (l != "    (see http://t.uber.com/nullaway )\n"):
            print(i)


def read_errors(path):
    lines = open(path, "r").readlines()
    error = None
    errors = []
    for i, l in enumerate(lines):
        if i % 4 == 2 or i % 4 == 3:
            continue
        if i % 4 == 0:
            error = l
        else:
            error += l
            errors.append(error)
            error = None
    return errors


def capture_errors_in_branches():
    for proj in projects['projects']:
        if not proj['active']:
            continue
        print("Working on {}".format(proj['name']))
        COMMAND = "cd {} && {}".format(root.format(proj['path']), "{}")
        os.system(COMMAND.format("git reset --hard"))
        os.system(COMMAND.format("git fetch"))
        for branch in ["nullaway", "deep_10"]:
            os.system(COMMAND.format("git checkout {}".format(branch)))
            os.system(COMMAND.format("git pull"))
            os.system("{} 2> error.txt".format(COMMAND.format(proj['build'])))
            if not os.path.exists("results/errors/{}".format(proj['name'])):
                os.system("mkdir results/errors/{}".format(proj['name']))
            os.system("mv {}/error.txt results/errors/{}/error_{}.txt".format(root.format(proj['path']), proj['name'],
                                                                              branch))


def sample_remaining_errors():
    for proj in projects['projects']:
        if not proj['active']:
            continue
        print("Working on {}".format(proj['name']))
        before = read_errors("results/errors/{}/error_nullaway.txt".format(proj['name']))
        after = read_errors("results/errors/{}/error_deep_10.txt".format(proj['name']))
        to_remove = []
        for e in after:
            if e not in before:
                to_remove.append(e)
        for e in to_remove:
            after.remove(e)
        sampled = random.sample(after, 5)
        with open("results/errors/{}/sampled.txt".format(proj['name']), "w") as f:
            f.writelines(sampled)
        exit()


sample_remaining_errors()