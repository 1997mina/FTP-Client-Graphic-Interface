#include <stdio.h>
int max (int arr[], int n)
{
    if (n == 1)
    return arr[0];
    else
    {
        if (arr[n - 1] > max (arr, n - 1))
        return arr[n - 1];
        else
        return max (arr, n - 1);
    }
}
int min (int arr[], int n)
{
    if (n == 1)
    return arr[0];
    else
    {
        if (arr[n - 1] < min (arr, n - 1))
        return arr[n - 1];
        else
        return min (arr, n - 1);
    }
}
int main ()
{
    int n;
    scanf ("%d", &n);
    int arr[n];
    for (int i = 0; i < n; i++)
    scanf ("%d", &arr[i]);
    printf ("max = %d\n", max (arr, n));
    printf ("min = %d\n", min (arr, n));
    return 0;
}