#include <iostream>
using namespace std;

int n, arr[10000], counter = 0;

bool check (int i, int k)
{
    for (int j = 1; j < k; j++)
    {
        if (arr[j - 1] == i)
        return false;
    }

    return true;
}

void write (int k)
{
    for (int i = 1; i <= n; i++)
    if (check (i, k))
    {
        arr[k - 1] = i;
        if (k == n)
        {
            cout << ++counter << ": ";
            for (int i = 0; i < n; i++)
                cout << arr[i] << " ";
            cout << endl;
        }
        else
        write (k + 1);
    }
}

int main ()
{
    cin >> n;
    write (1);

    return 0;
}
