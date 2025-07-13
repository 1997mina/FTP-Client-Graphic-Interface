#include <bits/stdc++.h>
using namespace std;

vector <bool> prime_table;
int n;

void Eratosthenes (int n)
{
    prime_table.assign (n + 1, true);
    int a = 2, k = a;
    while (a * a <= n)
    {
        while (a * k <= n)
        {
            prime_table.at (a * k) = false;
            k++;
        }
        do
        {
            a++;
        } while (!prime_table.at (a));
        k = a;
    }
}
int main () 
{
    cin >> n;
    Eratosthenes (n);
    for (int i = 2; i <= n; i++)
    {
        cout << i << ": "; 
        if (prime_table.at (i))
        cout << "Prime ";
        else
        cout << "Not a prime ";
        cout << "number" << endl;
    }
    return 0;
}