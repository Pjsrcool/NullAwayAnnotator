import os
import json
import time

config = json.load(open("config.json", "r"))
projects = json.load(open("projects.json", "r"))
root = "/home/nima/Developer/AutoFixer/Evaluation/Projects/{}"

# t: only_root, t: cache, t: bailout

for proj in projects['projects']:
    print("Working on {}".format(proj['name']))
    path = root.format(proj['path'])
    config['PROJECT_PATH'] = path
    config['BUILD_COMMAND'] = proj['build']
    config['ANNOTATION']['NULLABLE'] = proj['annot']['nullable']
    config['ANNOTATION']['INITIALIZER'] = proj['annot']['init']
    config['CACHE'] = False
    config['OPTIMIZED'] = True
    config['BAILOUT'] = False
    with open("config.json", "w") as f:
        json.dump(config, f)

    for i in range(0, 11):
        if i % 2 == 1:
            continue
        branch = "c_fff{}".format(i)
        COMMAND = "cd {} && {}".format(config['PROJECT_PATH'], "{}")
        os.system(COMMAND.format("git reset --hard"))
        os.system(COMMAND.format("git fetch"))
        os.system(COMMAND.format("git pull"))
        os.system(COMMAND.format("git checkout nullaway"))
        os.system(COMMAND.format("git pull"))
        os.system(COMMAND.format("git branch -D {}".format(branch)))
        os.system(COMMAND.format("git push origin --delete {}".format(branch)))
        os.system(COMMAND.format("git checkout -b {}".format(branch)))
        config["DEPTH"] = i
        with open("config.json", "w") as f:
            json.dump(config, f)

        start = time.time()
        os.system("python3 run.py run")
        duration = time.time() - start

        with open("{}/elapsed_time.txt".format(config['PROJECT_PATH']), "w") as f:
            f.write(str(duration))

        os.system("cd /tmp && zip -r annotator.zip NullAwayFix/")
        os.system("mv /tmp/annotator.zip {}/annotator.zip".format(config['PROJECT_PATH']))

        os.system(COMMAND.format("git add ."))
        os.system(COMMAND.format("git commit -m \"Final Result\""))
        os.system(COMMAND.format("git push --set-upstream origin {}".format(branch)))
