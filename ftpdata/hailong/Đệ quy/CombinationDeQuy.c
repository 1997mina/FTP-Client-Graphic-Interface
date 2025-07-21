// Liet ke tap con m phan tu cua tap n phan tu
#include <stdio.h>
int arr[1000], m, n, count = 0;

void Try (int k)
{
    for (int i = arr[k - 2] + 1; i <= n - m + k; i++)
    {
        arr[k - 1] = i;

        if (k == m)
        {
            printf ("%d: ", ++count);
            for (int i = 0; i < m; i++)
            printf ("%d ", arr[i]);
            printf ("\n");
        }

        else
        Try (k + 1);
    }
}

int main ()
{
    printf ("n = ");
    scanf ("%d", &n);
    printf ("m = ");
    scanf ("%d", &m);

    if (n >= m)
        Try (1);
    else
        printf ("Math Error");

    return 0;
}
