import { Button } from '@/shared/components/Button/Button';

interface PaginationProps {
  pageNumber: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ pageNumber, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="flex items-center justify-between gap-4 pt-2">
      <Button
        type="button"
        variant="secondary"
        disabled={pageNumber <= 0}
        onClick={() => onPageChange(pageNumber - 1)}
      >
        Précédent
      </Button>
      <span className="text-sm text-gray-500">
        Page {pageNumber + 1} / {totalPages}
      </span>
      <Button
        type="button"
        variant="secondary"
        disabled={pageNumber + 1 >= totalPages}
        onClick={() => onPageChange(pageNumber + 1)}
      >
        Suivant
      </Button>
    </div>
  );
}
