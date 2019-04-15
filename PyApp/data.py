
class Data:
    golfers = []
    groups = []
    days = []

    def __init__(self, golfers, groups, days):
        print("Constructor")
        self.days = days
        self.golfers = golfers
        self.groups = groups

    def golfersRule(self):
        return "Onc golfer per group and all golfers meet each other only once"

    def delGolfer(self, golfer):
        self.golfers.remove(golfer)

    def delGroup(self, group):
        self.days.remove(group)

    def addGolferToGroup(self, golfer, group):
        self.groups[group] = (golfer)

    def addGroupToDay(self, day, group):
        self.days[day] = (group)
