import { IonRouterOutlet } from '@ionic/react';
import { Route, Redirect } from 'react-router';
import PatientData from './PatientData';
import DeviceConnect from './DeviceConnect';
import Analysis from './Analysis';
import Results from './Results';

const ExamShell: React.FC = () => {
    return (
        <IonRouterOutlet>
            <Route exact path="/app/exam/patient" component={PatientData} />
            <Route exact path="/app/exam/connect" component={DeviceConnect} />
            <Route exact path="/app/exam/analysis" component={Analysis} />
            <Route exact path="/app/exam/results" component={Results} />
            <Route exact path="/app/exam">
                <Redirect to="/app/exam/patient" />
            </Route>
        </IonRouterOutlet>
    );
};

export default ExamShell;
