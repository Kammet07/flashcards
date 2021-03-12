import {Component, Input} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';
import {UserEntity} from '../../model/user.entity';
import {CollectionEntity} from '../../model/collection.entity';
import {FlashcardEntity} from '../../model/flashcard.entity';

@Component({
  selector: 'app-flashcards',
  templateUrl: './flashcards.component.html',
  styleUrls: ['./flashcards.component.scss']
})
export class FlashcardsComponent {
  activeFlashcardIndex = 0;
  flashcards: Array<FlashcardEntity> = [];
  private pCollection!: CollectionEntity;
  showDefinition = false;
  showCreate = false;
  showEdit = false;
  createTerm = '';
  createDefinition = '';
  editTerm = '';
  editDefinition = '';

  @Input()
  user: UserEntity | null = null;

  @Input()
  set collection(value: CollectionEntity) {
    this.loadFlashcards(value);
    this.pCollection = value;
  }

  get collection(): CollectionEntity {
    return this.pCollection;
  }

  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastr: ToastrService
  ) {
  }

  private loadFlashcards(collection: CollectionEntity): void {
    if (!collection) {
      this.toastr.error('Fetching flashcards went wrong: no collection given');
    } else {
      this.httpClient.get<FlashcardEntity[]>(`http://192.168.0.165:8080/api/collection/${collection.id}`, {withCredentials: true})
        .subscribe(u => {
          console.log(u);
          this.flashcards = u;
        }, error => {
          console.error(error);
        });
    }
  }

  createFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.post<FlashcardEntity>(`http://192.168.0.165:8080/api/collection/${this.collection.id}`, {
        term: this.createTerm,
        definition: this.createDefinition,
      }, {withCredentials: true}).subscribe(u => {
        console.log(u);
        this.flashcards.push(u);
        this.toastr.success(`Flashcard ${u.term} successfully added`);
        this.createTerm = '';
        this.createDefinition = '';
      }, error => {
        console.error(error);
        this.toastr.error(error.statusText, error.status);
      });
    } else {
      this.toastr.error('Something went wrong');
    }
  }

  editFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.put<FlashcardEntity>(`http://192.168.0.165:8080/api/flashcard/${this.collection.id}/${this.flashcards[this.activeFlashcardIndex].id}`, {
        id: this.flashcards[this.activeFlashcardIndex].id,
        term: this.editTerm,
        definition: this.editDefinition,
      }, {withCredentials: true}).subscribe(u => {
        console.log(u);
        this.toastr.success(`Flashcard ${u.term} successfully edited`);
        this.flashcards[this.activeFlashcardIndex] = u;
      }, error => {
        console.error(error);
        this.toastr.error(error.statusText, error.status);
      });
    } else {
      this.toastr.error('Something went wrong');
    }
  }

  deleteFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.delete(`http://192.168.0.165:8080/api/flashcard/${this.collection.id}/${this.flashcards[this.activeFlashcardIndex].id}`, {
        withCredentials: true,
        responseType: 'text'
      })
        .subscribe(() => {
          this.toastr.success('Flashcard has been removed');
          this.flashcards.slice(this.activeFlashcardIndex, 1);
        }, error => {
          this.toastr.error(error.responseText, error.response);
        });
    }
  }
}
