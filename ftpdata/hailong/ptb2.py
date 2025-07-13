import math
a = float (input ())
b = float (input ())
c = float (input ())
delta = b**2 - 4*a*c
if (a == 0):
  if (b != 0):
    print ("x =", -c/b)
  elif (b == 0 and c == 0):
    print ("Infinite solution")
  else:
    print ("No solution")
else:
  if (delta == 0):
    print ("x =", -b/(2*a))
  elif (delta > 0):
    print ("x1 =", (-b + math.sqrt (delta)) / (2*a))
    print ("x2 =", (-b - math.sqrt (delta)) / (2*a))
  else:
    print ("x1 =", -b/(2*a), "+", math.sqrt(math.fabs(delta))/(2*a), "i")
    print ("x2 =", -b/(2*a), "-", math.sqrt(math.fabs(delta))/(2*a), "i")