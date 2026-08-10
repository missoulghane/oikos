import { useEffect, useRef, useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty } from '@/features/identity/me';
import { useConversationMessages } from '@/features/messaging/hooks/useConversationMessages';
import { useMarkConversationRead } from '@/features/messaging/hooks/useMarkConversationRead';
import { MessageThreadItem } from '@/features/messaging/components/MessageThreadItem';
import { MessageComposer } from '@/features/messaging/components/MessageComposer';
import type { MessagingOutletContext } from '@/features/messaging/pages/MessagingLayout';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { ChevronLeftIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

// The reading pane of the Outlook-style split view - rendered inside
// MessagingLayout's <Outlet/>, next to the (always-visible on desktop)
// conversation list. On mobile the list is hidden while this is open, hence
// the back chevron below - it's the only header content: no subject/
// property/position banner repeating what the list row you just clicked
// already showed (that read as "un aperçu" sitting on top of the real
// content) - the page just IS the list of messages (sender, date, heure,
// body). List and detail are deliberately separate concerns: opening a
// received message is read-only until the user explicitly clicks
// "Répondre" - there is no always-on composer glued to the bottom of every
// thread.
export function ConversationPage() {
  const { conversationId } = useParams<{ conversationId: string }>();
  const id = conversationId as string;
  const currentUser = useCurrentUser();

  const messages = useConversationMessages(id);
  // No dedicated GET /conversations/:id endpoint in the wire contract -
  // canReply (type/propertyId) is resolved from the conversation list
  // already fetched by MessagingLayout (handed down via Outlet context),
  // rather than a second network round-trip.
  const { conversationList } = useOutletContext<MessagingOutletContext>();
  const markRead = useMarkConversationRead(id);
  const [isReplying, setIsReplying] = useState(false);

  // The component stays mounted across /messages/:id -> /messages/:otherId
  // navigations (same route, different param, e.g. clicking another row in
  // the list pane) rather than remounting - so per-conversation local state
  // has to be reset explicitly instead of relying on a fresh mount. "Adjust
  // state during rendering" (React's own recommended pattern for this)
  // rather than a useEffect, so it can't cascade an extra render.
  const [lastRenderedConversationId, setLastRenderedConversationId] = useState(id);
  if (id !== lastRenderedConversationId) {
    setLastRenderedConversationId(id);
    setIsReplying(false);
  }

  // Marking read is an actual network call, which does have to live in an
  // effect - guarded by the id itself (not a plain boolean) so it correctly
  // fires again for each newly opened conversation instead of only once
  // for the component's entire lifetime.
  const lastMarkedReadConversationId = useRef<string | null>(null);
  useEffect(() => {
    if (lastMarkedReadConversationId.current !== id) {
      lastMarkedReadConversationId.current = id;
      // Fire-and-forget: marking read must not block rendering the thread,
      // and the backend upserts the read marker so a duplicate call is safe.
      markRead.mutate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const summary = conversationList.find((conversation) => conversation.id === id);

  // Any participant may reply in a GROUP conversation (already enforced by
  // the backend for read access to even be here). A BROADCAST channel is
  // read-only for everyone except board/manager - mirrors
  // canSendToConversation server-side, so "Répondre" is never shown to
  // someone who'd get a 403 clicking it.
  const canReply =
    summary?.type === 'GROUP' ||
    (summary?.type === 'BROADCAST' &&
      Boolean(
        currentUser.data &&
          (isBoardTierOnProperty(currentUser.data, summary.propertyId) ||
            isManagerTierOnProperty(currentUser.data, summary.propertyId)),
      ));

  return (
    <div className="flex h-full flex-col">
      <Link
        to="/messages"
        aria-label="Retour à la liste des conversations"
        className="m-2 flex h-9 w-9 shrink-0 items-center justify-center self-start rounded-lg text-gray-500 hover:bg-gray-100 sm:hidden"
      >
        <ChevronLeftIcon className="h-5 w-5" />
      </Link>

      <div className="flex-1 overflow-y-auto">
        {messages.isLoading && <Loader label="Chargement des messages…" />}
        {messages.isError && (
          <div className="p-4">
            <Alert message={getErrorMessage(messages.error)} />
          </div>
        )}
        {messages.data?.content.map((message) => <MessageThreadItem key={message.id} message={message} />)}
      </div>

      {canReply && (
        <div className="border-t border-gray-200 p-3">
          {isReplying ? (
            <MessageComposer conversationId={id} />
          ) : (
            <Button type="button" variant="secondary" onClick={() => setIsReplying(true)}>
              Répondre
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
