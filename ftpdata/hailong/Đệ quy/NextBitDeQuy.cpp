#include <iostream>
using namespace std;
void write (string bit, int n)
{
    if (!n)
    cout << bit << endl;
    else
    {
        for (char i = '0'; i <= '1'; i++)
        write (bit + i, n - 1);
    }
}
int main () 
{
    int n;
    cin >> n;
    if (n <= 0)
    cout << "Math ERROR";
    else
    {
        cout << "Bit sequences:" << endl;
        write ("", n);
    }
    return 0;
}