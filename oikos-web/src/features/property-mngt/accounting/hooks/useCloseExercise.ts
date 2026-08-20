import { useMutation, useQueryClient } from '@tanstack/react-query';
import { closeExercise } from '@/features/property-mngt/accounting/api/closeExercise';
import { queryKeys } from '@/shared/constants/queryKeys';

/**
 * La clôture change bien plus que l'exercice : elle passe une écriture, donc
 * bouge les soldes, le grand livre et le journal. Tout ce qui touche à cette
 * copropriété est invalidé plutôt qu'une liste choisie de clés qu'on aurait
 * oublié d'allonger au prochain écran.
 */
export function useCloseExercise(propertyId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => closeExercise(propertyId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.detail(propertyId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.properties.accountingOpenExercise(propertyId) });
    },
  });
}
