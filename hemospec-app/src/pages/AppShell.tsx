import { IonTabs, IonRouterOutlet, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/react';
import { Route, Redirect } from 'react-router';
import { homeOutline, statsChartOutline, timeOutline } from 'ionicons/icons';
import Home from './Home';
import History from './History';
import Settings from './Settings';
import ExamShell from './exam/ExamShell';
import UserAccount from './UserAccount';
import AnalysisPage from './AnalysisPage';
import DebugPage from './DebugPage';

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
                <Route exact path="/app/statistics">
                    {/* Placeholder for Statistics, using History or Settings for now or empty */}
                    <div style={{ padding: 20 }}>Statistics Not Implemented</div>
                </Route>
                <Route exact path="/app/user-account">
                    <UserAccount />
                </Route>

                {/* Keep settings route but hide from tab if needed, or remove */}
                <Route exact path="/app/settings">
                    <Settings />
                </Route>

                <Route path="/app/exam" component={ExamShell} />

                {/* New Analysis Page Route - overriding exam shell for the demo flow */}
                <Route path="/app/analysis" component={AnalysisPage} />
                <Route path="/app/debug" component={DebugPage} />

                <Route exact path="/app">
                    <Redirect to="/app/home" />
                </Route>
            </IonRouterOutlet>

            <IonTabBar slot="bottom">
                <IonTabButton tab="home" href="/app/home">
                    <IonIcon icon={homeOutline} />
                    <IonLabel>Home</IonLabel>
                </IonTabButton>

                <IonTabButton tab="statistics" href="/app/statistics">
                    <IonIcon icon={statsChartOutline} />
                    <IonLabel>Statistics</IonLabel>
                </IonTabButton>

                <IonTabButton tab="history" href="/app/history">
                    <IonIcon icon={timeOutline} />
                    <IonLabel>History</IonLabel>
                </IonTabButton>
            </IonTabBar>
        </IonTabs>
    );
};

export default AppShell;
