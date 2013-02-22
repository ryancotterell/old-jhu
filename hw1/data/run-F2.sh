python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=Elevated
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=NotElevated

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes MaryVaccinated=No

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=Elevated
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=NotElevated
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=Elevated
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=NotElevated

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,MaryVaccinated=No
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,MaryVaccinated=No

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=Elevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=Elevated,MaryVaccinated=No
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=NotElevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes PreviousFluRate=NotElevated,MaryVaccinated=No

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=Elevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=Elevated,MaryVaccinated=No
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=NotElevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=Yes,PreviousFluRate=NotElevated,MaryVaccinated=No

python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=Elevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=Elevated,MaryVaccinated=No
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=NotElevated,MaryVaccinated=Yes
python bayes-query.py network-F2.txt cpd-F2.txt MaryGetsFlu=Yes IsFluSeason=No,PreviousFluRate=NotElevated,MaryVaccinated=No


