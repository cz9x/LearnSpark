# -*- coding:utf-8 -*-
import pandas as pd

team_elos = {}
team_stats = {}
base_elo = 1600

path = '/Users/tony/Documents/data/NBA/data/2015-2016_result.csv'

result_data = pd.read_csv(path, encoding="utf-8")
folder = '/Users/tony/Documents/data/NBA/data'
Mstat = pd.read_csv(folder + '/15-16Miscellaneous_Stat.csv')
Ostat = pd.read_csv(folder + '/15-16Opponent_Per_Game_Stat.csv')
Tstat = pd.read_csv(folder + '/15-16Team_Per_Game_Stat.csv')


def initialize_data(Mstat, Ostat, Tstat):
    new_Mstat = Mstat.drop(['Rk', 'Arena'], axis=1)
    new_Ostat = Ostat.drop(['Rk', 'G', 'MP'], axis=1)
    new_Tstat = Tstat.drop(['Rk', 'G', 'MP'], axis=1)

    team_stats1 = pd.merge(new_Mstat, new_Ostat, how='left', on='Team')
    team_stats1 = pd.merge(team_stats1, new_Tstat, how='left', on='Team')
    return team_stats1.set_index('Team', inplace=False, drop=True)

team_stats = initialize_data(Mstat, Ostat, Tstat)

# print team_stats
def get_elo(team):
    try:
        return team_elos[team]
    except:
        team_elos[team] = base_elo
        return team_elos[team]

for index, row in result_data.iterrows():
    Wteam = row['WTeam']
    Lteam = row['LTeam']

    team1_elo = get_elo(Wteam)
    team2_elo = get_elo(Lteam)

    team1_features = [team1_elo]
    team2_features = [team2_elo]

    for key, value in team_stats.loc[Wteam].iteritems():
        team1_features.append(value)
    for key, value in team_stats.loc[Lteam].iteritems():
        team2_features.append(value)

    print team1_features, team2_features

