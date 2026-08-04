import { QRCodeSVG } from 'qrcode.react';

interface InvitationLinkCardProps {
  link: string;
}

export function InvitationLinkCard({ link }: InvitationLinkCardProps) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-lg border border-gray-200 bg-gray-50 p-4 sm:flex-row">
      <QRCodeSVG value={link} size={128} className="shrink-0 rounded bg-white p-2" />
      <p className="break-all text-sm text-gray-500">{link}</p>
    </div>
  );
}
