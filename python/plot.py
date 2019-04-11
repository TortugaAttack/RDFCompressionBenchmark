import matplotlib.pyplot as plt
import statistics as st

# HDT
lists = []

lists.append([
0.05383796842196083, 0.03656303624668066, 0.03854744854428499, 0.03936230383557179, 0.04038806284930929, 0.041557619855626816, 0.04364748401445649, 0.04466365649535532, 0.04660013612876631, 0.04765465474101981, 0.04839281776959727, 0.05056896072397496, 0.05214115210951655, 0.0534257475098981, 0.05479662170582765, 0.05570734232550113, 0.05473910250879564, 0.052917661269448676, 0.05276427674402999, 0.05053061459262029, 0.049619893972946805, 0.04788473152914785, 0.04660972266160498, 0.04479786795509668, 0.04435688744451794, 0.042171157957301586, 0.04148092759291747, 0.04080028376137203, 0.03875835226673569, 0.03770383365448218, 




    ])


# GRP
lists.append([

0.03538389270752447, 0.05708780305426936, 0.0595803015923231, 0.06074985859864063, 0.06166057921831411, 0.06239874224689157, 0.06307938607843701, 0.06324235713669438, 0.06363540498307976, 0.06406679896081983, 0.06433522188030255, 0.06431604881462522, 0.06479537545655863, 0.06475702932520395, 0.06491041385062264, 0.06535139436120138, 0.06538015395971739, 0.06508297144171868, 0.06494875998197731, 0.0644981929385599, 0.0644119141430119, 0.06435439494597989, 0.06364499151591843, 0.06315607834114635, 0.06257129983798759, 0.06081696432851131, 0.06059647407322194, 0.059963762905869834, 0.05869834057116562, 0.056138736303241205, 





])

if lists.__len__() == 2:
    assert lists[0].__len__() == lists[1].__len__()

if lists.__len__() == 2:
    lstDiff = []
    for i in range(lists[0].__len__()):
        lstDiff.append(lists[1][i] / lists[0][i])

    print("mean: ", st.mean(lstDiff))

for i in range(lists.__len__()):
    plt.plot(lists[i])
    print('std dev i: ', st.stdev(lists[i]))

legends = ['HDT', 'GRP']
plt.legend(legends)

plt.ylabel('compr. ratio')
plt.xlabel('<--  hub pattern           authority pattern -->')
plt.show()
