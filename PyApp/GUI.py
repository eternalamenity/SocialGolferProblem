from tkinter import *

window = Tk()

window.title("Social Golfer Problem Solver")

window.geometry('640x480')

lbl1 = Label(window, text="golfers=",width=10,height=3,anchor=S)
lbl2 = Label(window, text="groups=",width=10,height=3,anchor=S)
lbl3 = Label(window, text="days=",width=10,height=3,anchor=S)

lbl1.grid(column=0, row=0)
lbl2.grid(column=0, row=1)
lbl3.grid(column=0, row=2)
txt1 = Entry(window, width=10)
txt2 = Entry(window, width=10)
txt3 = Entry(window, width=10)

txt1.grid(column=1, row=0)
txt2.grid(column=1, row=1)
txt3.grid(column=1, row=2)


btn1 = Button(window, text="Solve!",width=10)
btn1.grid(column=2,row=9)



window.mainloop()