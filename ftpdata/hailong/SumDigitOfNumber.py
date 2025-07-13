def make_line ():
    for i in range (50):
        print ('-', end = '')
    print ()
num = int (input ("Enter a number: "))
make_line ()
if (num < 0):
    print ("Math ERROR")
else:
    d = 0
    while (num != 0):
        k = num % 10
        d += k
        num //= 10
    print ("Sum of digits: " + str(d))