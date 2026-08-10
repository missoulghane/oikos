import { useEffect, useRef, useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty } from '@/features/identity/me';
import { useConversationMessages } from '@/features/messaging/hooks/useConversationMessages';
import { useMarkConversationRead } from '@/features/messaging/hooks/useMarkConversationRead';
import { MessageThreadItem } from '@/features/messaging/components/MessageThreadItem';
import { MessageComposer } from '@/features/messaging/components/MessageComposer';
import type { MessagingOutletContext } from '@/features/messaging/pages/MessagingLayout';
import { BOX_PATH } from '@/features/messaging/utils/boxPath';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { ChevronLeftIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

// The reading pane - rendered inside MessagingLayout's <Outlet/>, replacing
// the conversation list entirely (single-pane, never side by side - see
// MessagingLayout), so the back chevron toolbar is the only way back at any
// screen size, not just mobile. No subject/property/position banner
// repeating what the list row you just clicked already showed (that read as
// "un aperçu" sitting on top of the real content) - the page just IS the
// list of messages (sender, date, heure, body). List and detail are
// deliberately separate concerns: opening a received message is read-only
// until the user explicitly clicks "Répondre", at the bottom of the thread
// (pill button, only action available - no reply-all/forward, a GROUP reply
// already goes to every participant and forwarding isn't a feature this app
// has).
export function ConversationPage() {
  const { conversationId } = useParams<{ conversationId: string }>();
  const id = conversationId as string;
  const currentUser = useCurrentUser();

  const messages = useConversationMessages(id);
  // No dedicated GET /conversations/:id endpoint in the wire contract -
  // canReply (type/propertyId) is resolved from the conversation list
  // already fetched by MessagingLayout (handed down via Outlet context),
  // rather than a second network round-trip.
  const { conversationList, box } = useOutletContext<MessagingOutletContext>();
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
      <div className="flex items-center gap-2 border-b border-gray-100 dark:border-gray-800 p-3">
        <Link
          to={BOX_PATH[box]}
          aria-label="Retour à la liste des messages"
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-gray-200 dark:border-gray-800 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
        >
          <ChevronLeftIcon className="h-5 w-5" />
        </Link>
      </div>

      <div className="flex-1 overflow-y-auto p-4">
        {messages.isLoading && <Loader label="Chargement des messages…" />}
        {messages.isError && (
          <div className="p-4">
            <Alert message={getErrorMessage(messages.error)} />
          </div>
        )}
        {messages.data && messages.data.content.length > 0 && (
          <div className="flex flex-col gap-3">
            {messages.data.content.map((message) => (
              <MessageThreadItem key={message.id} message={message} />
            ))}
          </div>
        )}
      </div>

      {canReply && (
        <div className="border-t border-gray-100 dark:border-gray-800 p-3">
          {isReplying ? (
            <MessageComposer conversationId={id} />
          ) : (
            <Button
              type="button"
              variant="secondary"
              onClick={() => setIsReplying(true)}
              className="rounded-full"
            >
              Répondre
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
