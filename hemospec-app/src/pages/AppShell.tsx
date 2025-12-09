import { IonTabs, IonRouterOutlet, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/react';
import { Route, Redirect } from 'react-router';
import { homeOutline, timeOutline, settingsOutline, addCircleOutline } from 'ionicons/icons';
import Home from './Home';
import History from './History';
import Settings from './Settings';
import ExamShell from './exam/ExamShell';

const AppShell: React.FC = () => {
    return (
        <IonTabs>
            <IonRouterOutlet>
                <Route exact path="/app/home">
                    <Home />
                </Route>
                <Route exact path="/app/history">
                    <History />
                </Route>
                <Route exact path="/app/settings">
                    <Settings />
                </Route>
                <Route path="/app/exam" component={ExamShell} />

                <Route exact path="/app">
                    <Redirect to="/app/home" />
                </Route>
            </IonRouterOutlet>

            <IonTabBar slot="bottom">
                <IonTabButton tab="home" href="/app/home">
                    <IonIcon icon={homeOutline} />
                    <IonLabel>Home</IonLabel>
                </IonTabButton>

                {/* Central Action Button Style */}
                <IonTabButton tab="exam" href="/app/exam/patient">
                    <IonIcon icon={addCircleOutline} style={{ fontSize: '2rem', color: 'var(--ion-color-primary)' }} />
                    <IonLabel>New Exam</IonLabel>
                </IonTabButton>

                <IonTabButton tab="history" href="/app/history">
                    <IonIcon icon={timeOutline} />
                    <IonLabel>History</IonLabel>
                </IonTabButton>

                <IonTabButton tab="settings" href="/app/settings">
                    <IonIcon icon={settingsOutline} />
                    <IonLabel>Settings</IonLabel>
                </IonTabButton>
            </IonTabBar>
        </IonTabs>
    );
};

export default AppShell;
