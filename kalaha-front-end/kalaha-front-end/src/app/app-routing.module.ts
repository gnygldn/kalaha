import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { GameComponent } from './game/game.component';
import { MainComponent } from './main/main.component';


const routes: Routes = [
  {
    path:'', 
    component: MainComponent
  },
  {
    path:'game/:gameId', 
    component: GameComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {preloadingStrategy:PreloadAllModules})
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
