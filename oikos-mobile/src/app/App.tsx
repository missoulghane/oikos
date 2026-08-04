import { StatusBar } from 'expo-status-bar';
import { AppProviders } from '@/app/providers';
import { RootNavigator } from '@/app/navigation/RootNavigator';

export default function App() {
  return (
    <AppProviders>
      <RootNavigator />
      <StatusBar style="auto" />
    </AppProviders>
  );
}
