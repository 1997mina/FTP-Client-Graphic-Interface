#include <iostream>
int GCD (unsigned int a, unsigned int b)
{
  while (a != b)
  {
    if (a > b)
    a -= b;
    else
    b -= a;
  }
  return a;
}
int LCM (unsigned int a, unsigned int b)
{
  return a*b/GCD (a, b);
}
int main ()
{
  unsigned int x, y;
  std::cin >> x;
  std::cin >> y;
  std::cout << "GCD (" << x << "," << y << ") = " << GCD (x, y) << std::endl;
  std::cout << "LCM (" << x << "," << y << ") = " << LCM (x, y);
  return 0;
}